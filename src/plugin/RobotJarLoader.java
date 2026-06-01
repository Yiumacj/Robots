package plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RobotJarLoader implements AutoCloseable
{
    private final URLClassLoader classLoader;

    public RobotJarLoader(File jarFile) throws IOException
    {
        validateJarFile(jarFile);
        URL url = jarFile.toURI().toURL();
        classLoader = new URLClassLoader(new URL[] { url }, RobotProgram.class.getClassLoader());
    }

    public static List<String> findProgramClassNames(File jarFile) throws IOException
    {
        validateJarFile(jarFile);
        RobotJarLoader loader = new RobotJarLoader(jarFile);
        try
        {
            return loader.findProgramClassNames(jarFile, false);
        }
        finally
        {
            loader.close();
        }
    }

    public RobotProgram createProgram(File jarFile, String className) throws Exception
    {
        validateJarFile(jarFile);
        Class<?> type = classLoader.loadClass(className);
        if (RobotProgram.class.isAssignableFrom(type))
        {
            Constructor<?> constructor = type.getDeclaredConstructor();
            if (!constructor.isAccessible())
            {
                constructor.setAccessible(true);
            }
            return (RobotProgram) constructor.newInstance();
        }
        Method method = findRobotMethod(type);
        if (method == null)
        {
            throw new RobotJarException("Class is not a robot program: " + className);
        }
        Constructor<?> constructor = type.getDeclaredConstructor();
        if (!constructor.isAccessible())
        {
            constructor.setAccessible(true);
        }
        Object instance = constructor.newInstance();
        if (!method.isAccessible())
        {
            method.setAccessible(true);
        }
        return context ->
        {
            try
            {
                method.invoke(instance, context);
            }
            catch (InvocationTargetException e)
            {
                Throwable target = e.getTargetException();
                if (target instanceof Exception)
                {
                    throw (Exception) target;
                }
                if (target instanceof Error)
                {
                    throw (Error) target;
                }
                throw e;
            }
        };
    }

    public List<String> findProgramClassNames(File jarFile, boolean failOnBrokenClass) throws IOException
    {
        validateJarFile(jarFile);
        List<String> classNames = new ArrayList<String>();
        JarFile jar = new JarFile(jarFile);
        try
        {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
                if (!isLoadableClass(entry))
                {
                    continue;
                }
                String className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                try
                {
                    Class<?> type = Class.forName(className, false, classLoader);
                    if (isRobotClass(type))
                    {
                        classNames.add(className);
                    }
                }
                catch (LinkageError error)
                {
                    if (failOnBrokenClass)
                    {
                        throw error;
                    }
                }
                catch (ClassNotFoundException e)
                {
                    if (failOnBrokenClass)
                    {
                        throw new RobotJarException("Cannot load class: " + className, e);
                    }
                }
            }
        }
        finally
        {
            jar.close();
        }
        Collections.sort(classNames);
        return classNames;
    }

    @Override
    public void close() throws IOException
    {
        classLoader.close();
    }

    private static boolean isRobotClass(Class<?> type)
    {
        return !type.isInterface() && (RobotProgram.class.isAssignableFrom(type) || findRobotMethod(type) != null);
    }

    private static Method findRobotMethod(Class<?> type)
    {
        Method method = findDeclaredMethod(type, "run");
        if (method != null)
        {
            return method;
        }
        return findDeclaredMethod(type, "start");
    }

    private static Method findDeclaredMethod(Class<?> type, String name)
    {
        try
        {
            return type.getDeclaredMethod(name, RobotContext.class);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    private static void validateJarFile(File file) throws IOException
    {
        if (file == null)
        {
            throw new RobotJarException("Robot file is not selected.");
        }
        if (!file.isFile())
        {
            throw new RobotJarException("Robot file does not exist: " + file);
        }
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".jar"))
        {
            throw new RobotJarException("Selected file is not a .jar archive: " + file.getName());
        }
        JarFile jar = new JarFile(file);
        jar.close();
    }

    private static boolean isLoadableClass(JarEntry entry)
    {
        String name = entry.getName();
        return !entry.isDirectory()
                && name.endsWith(".class")
                && !name.endsWith("module-info.class")
                && !name.endsWith("package-info.class")
                && !name.contains("$");
    }
}
