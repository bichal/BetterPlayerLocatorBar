Get-ChildItem -Recurse -Include *.png | ForEach-Object {
    magick $_.FullName -colorspace Gray -strip -define png:compression-level=9 -define png:compression-filter=0 -define png:compression-strategy=1 -define png:compression-memory-level=9 -interlace Plane -quality 100 $_.FullName
    oxipng -o 6 --strip all --zopfli --fix $_.FullName
}
