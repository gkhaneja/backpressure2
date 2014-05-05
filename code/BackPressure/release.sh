cd Backpressure/
./compile.sh
cd ..
scp -r BackPressure cs525@192.17.237.97:/home/cs525/Project/code/
ssh cs525@192.17.237.97 'cd Project/scripts/; bash copyAndRun.sh IPFiles/IPs-4.txt hc2b'
