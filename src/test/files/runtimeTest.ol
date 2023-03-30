from runtime import Runtime
from console import Console

service MyService() {
	execution{ single }
	
	embed Runtime as Runtime
	embed Console as Console

	main {
		x = 10
		i = 0

		while(i < 3){
			x.y << x
			i++
		}

		dumpState@Runtime()(s1)
		print@Console(s1)()
	}
}