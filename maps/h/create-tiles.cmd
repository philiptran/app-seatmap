@echo off

%~d0
cd %~p0


set orig=h.orig.png
set output=h.jpg

echo ========== Resize orig image of 877w to fit screen size of 1280w ========== 
convert %orig% -filter Lanczos -resize 175%% %output%

echo ========== Create Tiles ==========

set filename=h.jpg

set /a tilesize=256
set /a downsamplesize=1000

set tilesfolder=tiles
set downsamplesfolder=samples

mkdir %tilesfolder% %downsamplesfolder%

echo Generate 3/4
convert %filename% -resize 75%%  750.jpg
echo Generate 1/2
convert %filename% -resize 50%%  500.jpg

echo Generate the smaller map images (divide by 2 each time)
convert %filename% -resize %downsamplesize%x%downsamplesize%  ./%downsamplesfolder%/h.jpg


echo Generate the downsample map
convert %filename% -crop %tilesize%x%tilesize% -set filename:tile "%%[fx:page.x/%tilesize%]_%%[fx:page.y/%tilesize%]" +repage +adjoin "./%tilesfolder%/h_1000_%%[filename:tile].png"

echo Generate the tile for the half size map
convert 750.jpg -crop %tilesize%x%tilesize% -set filename:tile "%%[fx:page.x/%tilesize%]_%%[fx:page.y/%tilesize%]" +repage +adjoin "./%tilesfolder%/h_750_%%[filename:tile].png"

echo Generate the tile for the half size map
convert 500.jpg -crop %tilesize%x%tilesize% -set filename:tile "%%[fx:page.x/%tilesize%]_%%[fx:page.y/%tilesize%]" +repage +adjoin "./%tilesfolder%/h_500_%%[filename:tile].png"

echo Clean up

del 500.jpg 750.jpg

echo DONE
pause