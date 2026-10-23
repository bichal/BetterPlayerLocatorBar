plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-fabric" /* [SC] DO NOT EDIT */

stonecutter parameters {
    val loader = node.project.name.substringAfterLast('-')
    constants.match(loader, "fabric", "neoforge")
    // 1.21.6 added the vanilla locator bar (waypoints); from there the mod customises it instead of tracking players itself.
    constants["waypoints"] = eval(node.metadata.version, ">=1.21.6")
}
