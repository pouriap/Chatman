taskkill /F /IM ncat.exe
ncat --listen -p 9988 --keep-open --sh-exec "java -jar chatman.jar server"