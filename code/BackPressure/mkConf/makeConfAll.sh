# TopoFile randomStr IPList trafficType

#assign port numbers to connections
rm confFiles/$2-ports
cat $1 | awk '{print $0"\t"5000+NR}' > confFiles/$2-ports

#compute shortest paths
rm confFiles/$2-pathLengths
nodes=`cat $1 | awk 'BEGIN{max=0}{if ($1>max) max=$1; else if ($2>max) max=$2;}END{print max+1}'`
./shortestPath $1 $nodes
mv pathLengths.txt confFiles/$2-pathLengths

#generate flowIDS
# 0 all-to-all
# 1 random permutation
rm confFiles/$2-flows
flowID=0
if [ $4 -eq 0 ]; then
	for (( i=0; i<$nodes; i++))
	do
		for (( j=0; j<$nodes ; j++))
		do
			if [ $i -ne $j ]; then
				echo -e $i"\t"$j"\t"$flowID >> confFiles/$2-flows
				flowID=$((flowID+1))
			fi 	
		done
	done
fi

# input - nodeNumber randomStr IPFile flowRate payload
for (( i=0; i<$nodes; i++))
do
	bash makeConfPerNode.sh $i $2 $3 1 100 
done
