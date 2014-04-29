/*
 * main.cpp
 *
 *  Created on: Apr 24, 2014
 *      Author: ajsangeetha
 */
#include <iostream>
#include <fstream>
#include <stdio.h>
#include <string>
#include <stdlib.h>
#include <string>

using namespace std;

int main( int argc, const char* argv[] ){
	int topoSizeGlobal;
	string fileName;
	for (int i = 1; i < argc; i++){
		fileName = argv[1];
		topoSizeGlobal = atoi(argv[2]);
	}
	int graph[topoSizeGlobal][topoSizeGlobal];
	for (int i = 0; i < topoSizeGlobal; i++){
		for (int j = 0; j < topoSizeGlobal; j++){
			if (i==j)
				graph[i][j] = 0;
			else
				graph[i][j] = 10000;
		}
	}

//	std::cout << "here2\n";

	std::ifstream inFile( fileName.c_str() );
	while( inFile.is_open() ) {
		std::string line;
		getline( inFile, line, ' ' );
		int node1 = atoi( line.c_str() );
		getline( inFile, line );
		int node2 = atoi( line.c_str() );

		graph[node1][node2] = 1;
		graph[node2][node1] = 1;

		if( inFile.peek() == EOF )
			inFile.close();
	}
//	std::cout << "here3\n";
//
//	int dist[topoSizeGlobal][topoSizeGlobal];
//	std::cout << "here4\n";
//    for (int i = 0; i < topoSizeGlobal; i++){
//        for (int j = 0; j < topoSizeGlobal; j++){
//          //  dist[i][j] = graph[i][j];
//            std::cout << " element " << i << " " << j << " " << graph[i][j] << "\n";
//        }
//    }
//	std::cout << "here5\n";
    for (int k = 0; k < topoSizeGlobal; k++)
    {
        for (int i = 0; i < topoSizeGlobal; i++)
        {
            for (int j = 0; j < topoSizeGlobal; j++)
            {
                if (graph[i][k] + graph[k][j] < graph[i][j]){
                	graph[i][j] = graph[i][k] + graph[k][j];
                }
            }
        }
    }

    ofstream outFile;
    outFile.open("pathLengths.txt");
    for (int i = 0; i < topoSizeGlobal; i++)
    {
        for (int j = 0; j < topoSizeGlobal; j++)
        {
        	outFile << i << "\t" << j << "\t" << graph[i][j] << "\n";
        }
    }
    outFile.close();

}
