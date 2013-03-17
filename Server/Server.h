/*
 *
 *  @author : yassine
 */

#ifndef SERVER_H_
#define SERVER_H_

#include <vector>
#include <exception>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h> /* close */
#include <netdb.h> /* gethostbyname */

#define INVALID_SOCKET 			-1
#define SOCKET_ERROR 			-1
#define BUFFER_SIZE			 	1024

namespace nsServer
{

	class Server
	{
		public :

			Server();
			int 	init() throw (std::exception);
			int 	readClient (int socket, sockaddr sa,char * buffer);
			int 	removeClient (int toRemove);
			void 	disconnect();

		private :

			char 		_buffer [BUFFER_SIZE];
			int  		_socket;
			int			_port;
			int			_maxClients;
			sockaddr_in _sin;  // paramètres de la socket (IPV4, IPV6, port etc.)


	}; //Server

} // namespace nsServer

#endif /* SERVER_H_ */
