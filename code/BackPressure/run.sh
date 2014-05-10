#newt "java -classpath bin/ parn.main.Main 1" "java -classpath bin/ parn.main.Main 2" "java -classpath bin/ parn.main.Main 3 | grep ShadowQueue"
pkill -9 java
./compile.sh
java -Xmx400m -Xms400m -classpath bin/ parn.main.Main mkConf/confFiles/0-hc2b-conf.txt >out0 2>error0 &
java -Xmx400m -Xms400m -classpath bin/ parn.main.Main mkConf/confFiles/1-hc2b-conf.txt >out1 2>error1 &
java -Xmx400m -Xms400m -classpath bin/ parn.main.Main mkConf/confFiles/2-hc2b-conf.txt >out2 2>error2 &
java -Xmx400m -Xms400m -classpath bin/ parn.main.Main mkConf/confFiles/3-hc2b-conf.txt >out3 2>error3 &
