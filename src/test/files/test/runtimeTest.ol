from runtime import Runtime
from console import Console
from math import Math

service MyService() {
	embed Runtime as Runtime
	embed Console as Console
	embed Math as Math

	main {
		random@Math(  )(  )
		
		dumpState@Runtime()(s1)
		print@Console(s1)()
	}
}