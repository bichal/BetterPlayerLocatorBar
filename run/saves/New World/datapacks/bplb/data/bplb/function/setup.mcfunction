scoreboard objectives add bplb_system dummy "BPLB System"
scoreboard objectives add bplb_x dummy "BPLB X"
scoreboard objectives add bplb_y dummy "BPLB Y"
scoreboard objectives add bplb_z dummy "BPLB Z"
scoreboard players set #bplb_enabled bplb_system 1
scoreboard players set #bplb_tick bplb_system 0
tellraw @a {"text":"[BPLB] Datapack initialized!","color":"green"}
