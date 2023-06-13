bg size: 500x600
max popup size: 350x400
imagemagick resize gif: convert input.gif -coalesce -resize 70% output.gif
imagemagick fix gif animation: convert -delay 50 -loop 0 animation1.gif animation2.gif
delay is speed of animation
loop 0 means infinite loop 
wave file should be signed 16-bit PCM