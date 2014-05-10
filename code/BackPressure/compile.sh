cp -r ~/Documents/workspace/BackPressureProd/src/parn/ ~/Documents/prod/backpressure2/trunk/code/BackPressure/src/parn/
#cp -r ~/Documents/workspace/BackPressureProd/bp.conf ~/Documents/prod/backpressure2/trunk/code/BackPressure/bp.conf
find src -name "*.java" > sources.txt
javac -d bin/ @sources.txt
