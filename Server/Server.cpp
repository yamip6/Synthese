/*
 * Server.cpp
 *
 *  Created on: 17 mars 2013
 *      Author: yassine
 */

#include "Server.h"

#define SRV nsServer::Server

using namespace std;

SRV::Server()
{
	_socket = socket(AF_INET, SOCK_STREAM, 0);
	_sin	= {0};
	_port	= 9000;
	_maxClients = 20;
} // Server ()

int SRV::init () throw (exception)
{

	if (_socket == INVALID_SOCKET) throw exception("Impossible to create socket");

	 _sin.sin_addr.s_addr = htonl(INADDR_ANY); // on accepte n'importe quel port (client)
	 _sin.sin_port = htons(_port);			   // port serveur
	 _sin.sin_family = AF_INET;					// ipV4

	   if(bind(_socket,(sockaddr *) &_sin, sizeof _sin) == SOCKET_ERROR) throw exception("problem socket's options");


	   if(listen(_socket, _maxClients) == SOCKET_ERROR) throw exception("Waiting clients problem");

	   return _socket;

} // init ()

void SRV::disconnect()
{
	close(_socket);
} // disconnect ()
