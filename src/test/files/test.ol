from runtime import Runtime
from console import Console

service MyService() {
	execution{ single }
	
	embed Runtime as Runtime
	embed Console as Console

	main {
		a = int(b)
		// while(a >= 0){
		// 	a -= 1
		// }
		
		dumpState@Runtime()(s1)
		print@Console(s1)()
	}
}