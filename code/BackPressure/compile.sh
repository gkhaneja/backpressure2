cp -r ~/Documents/workspace/BackPressure/src/parn/ ~/Documents/backpressure2/trunk/code/BackPressure/src/parn/
cp -r ~/Documents/workspace/BackPressure/bp.conf ~/Documents/backpressure2/trunk/code/BackPressure/bp.conf
find src -name "*.java" > sources.txt
javac -d bin/ @sources.txt
