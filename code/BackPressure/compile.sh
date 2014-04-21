cp -r ~/Documents/workspace/BackPressure/src/parn/ ~/Documents/backpressure2/trunk/code/BackPressure/src/parn/
find src -name "*.java" > sources.txt
javac -d bin/ @sources.txt
