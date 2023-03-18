from runtime import Runtime
from console import Console

service MyService() {
	execution{ single }
	
	embed Runtime as Runtime
	embed Console as Console

	main {
		a[0] = 10
		a[1] = "20"

		dumpState@Runtime()(s1)
		print@Console(s1)()
	}
}