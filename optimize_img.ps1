Get-ChildItem -Recurse -Include *.png | ForEach-Object {
    magick $_.FullName -strip -define png:compression-level=9 -define png:compression-filter=0 -define png:compression-strategy=1 -define png:compression-memory-level=9 -sampling-factor 4:2:0 -colorspace sRGB -interlace Plane -quality 100 $_.FullName
    oxipng -o 6 --strip all --zopfli --fix $_.FullName
}
