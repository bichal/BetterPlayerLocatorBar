plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-fabric" /* [SC] DO NOT EDIT */

stonecutter parameters {
    val loader = node.project.name.substringAfterLast('-')
    constants.match(loader, "fabric", "neoforge")
}
