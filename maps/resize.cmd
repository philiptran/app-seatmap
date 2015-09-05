@echo off

%~d0
cd %~p0


set orig=map-pin.png
set output=map-pin-red.png

echo ========== Resize orig image of 877w to fit screen size of 1280w ========== 
convert %orig% -filter Lanczos -resize 50%% %output%

echo DONE
pause