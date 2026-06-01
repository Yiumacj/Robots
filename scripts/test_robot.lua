-- Test robot script.
-- Supported commands:
--   moveTo(x, y) / move_to(x, y) / target(x, y)
--   wait(ms) / sleep(ms)
--   color(r, g, b) / setColor(r, g, b) / set_color(r, g, b)
--   for i = 1, 3 do ... end

color(255, 0, 0)
moveTo(250, 120)
wait(1200)

color(0, 120, 255)
moveTo(450, 260)
wait(1400)

for i = 1, 2 do
    color(255, 220, 0)
    moveTo(130, 330)
    wait(1000)

    color(0, 220, 80)
    moveTo(500, 120)
    wait(1000)
end

color(255, 0, 255)
moveTo(200, 200)
