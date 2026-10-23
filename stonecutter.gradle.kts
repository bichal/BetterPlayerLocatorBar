plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-fabric" /* [SC] DO NOT EDIT */

stonecutter parameters {
    val loader = node.project.name.substringAfterLast('-')
    constants.match(loader, "fabric", "neoforge")
    // 1.21.6 added the vanilla locator bar (waypoints); from there the mod customises it instead of tracking players itself.
    // 1.21.11 renamed ResourceLocation to Identifier
    replacements.string(eval(node.metadata.version, ">=1.21.11")) {
        replace("ResourceLocation", "Identifier")
    }
    // 26.1 replaced immediate-mode GuiGraphics with GuiGraphicsExtractor (drawing calls renamed)
    replacements.string(eval(node.metadata.version, ">=26.1")) {
        replace("GuiGraphics", "GuiGraphicsExtractor")
        replace(".drawString(", ".text(")
        replace(".drawCenteredString(", ".centeredText(")
    }
    // 26.2: the locator bar renderer lost its suffix and setScreen became setScreenAndShow
    replacements.string(eval(node.metadata.version, ">=26.2")) {
        replace("LocatorBarRenderer", "LocatorBar")
        replace(".setScreen(", ".setScreenAndShow(")
    }
    constants["waypoints"] = eval(node.metadata.version, ">=1.21.6")
}
