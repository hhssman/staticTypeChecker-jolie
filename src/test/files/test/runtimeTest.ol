from runtime import Runtime
from console import Console

service MyService() {
	execution{ single }
	
	embed Runtime as Runtime
	embed Console as Console

	main {
		x = 10L
		x[1] = "hey"
		x[2] = true

		y = 5

		i = 0
		while(i < 2){
			y = x[i]
			i++
		}

		dumpState@Runtime()(s1)
		print@Console(s1)()
	}
}