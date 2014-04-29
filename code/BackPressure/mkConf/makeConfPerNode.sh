# input - nodeNumber randomStr IPFile flowRate payload

rm confFiles/$1-$2-conf.txt
# copying parameters
cat params | awk '{print $0}' > confFiles/$1-$2-conf.txt

# Node nodenumber
cat $3 | awk -v node="$1" '{if ($1 == node) print "Node\t"$1"\nIP\t"$2}' >> confFiles/$1-$2-conf.txt

#Neighbors number of neighbors
neighbors=`cat confFiles/$2-ports | awk -v node="$1" 'BEGIN{nei=0}{if(node==$1 || node==$2)nei++}END{print nei}'`
echo -e "Neighbors\t"$neighbors >> confFiles/$1-$2-conf.txt

#NeighborID neighborIP portNumber
cat confFiles/$2-ports | awk -v node="$1" '{if(node==$1) print $2"\t"$3; else if(node==$2) print $1"\t"$3}' > temp.txt
awk 'NR==FNR{a[$1]=$2; next} {print $1"\t"a[$1]"\t"$2 }'  $3 temp.txt  >> confFiles/$1-$2-conf.txt
rm temp.txt

#Destination no:
dest=`cat $3 | awk 'BEGIN{max=0}{if ($1>max) max=$1}END{print max}'`
echo -e "Destination\t"$dest >> confFiles/$1-$2-conf.txt

#DestID    DestIP PathLength
cat confFiles/$2-pathLengths | awk -v node="$1" '{if ($1==node && $2!=$1){print $2"\t"$3} }' >> temp.txt
awk 'NR==FNR{a[$1]=$2; next} {print $1"\t"a[$1]"\t"$2 }'  $3 temp.txt >> confFiles/$1-$2-conf.txt
rm temp.txt

#flows number
flows=`cat confFiles/$2-flows | awk -v node="$1" 'BEGIN{flows=0}{if ($1==node)flows++}END{print flows}'`
echo -e "Flows\t"$flows >> confFiles/$1-$2-conf.txt

#FlowID destinationNode flowRate (packets per second) payload (number of payload bytes)
cat confFiles/$2-flows | awk -v node="$1" -v rate="$4" -v payload="$5" '{if ($1==node) print $3"\t"$2"\t"rate"\t"payload}' >> confFiles/$1-$2-conf.txt
