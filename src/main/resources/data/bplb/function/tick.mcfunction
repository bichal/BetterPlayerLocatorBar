execute unless score #bplb_enabled bplb_system matches 1 run return 0

scoreboard players add #bplb_tick bplb_system 1
execute if score #bplb_tick bplb_system matches 2.. run function bplb:update_positions
execute if score #bplb_tick bplb_system matches 2.. run scoreboard players set #bplb_tick bplb_system 0
