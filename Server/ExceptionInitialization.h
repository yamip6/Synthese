/*
 * ExceptionInitialization.h

 *
 *  Created on: 17 mars 2013
 *      Author: yassine
 */

#include <exception>

#ifndef EXCEPTIONINITIALIZATION_H_
#define EXCEPTIONINITIALIZATION_H_

using namespace std;

namespace nsExceptions
{

	class ExceptionInitialization : public exception
	{
		public :

			ExceptionInitialization();
			string getMessage();

	}; // ExceptionInitialization

} // namespace nsExceptions


#endif /* EXCEPTIONINITIALIZATION_H_ */
