from runtime import Runtime
from console import Console

type A: int {
	choice: int | string
}

type B: string {
	s: string
}

type C: int {
	i: int
}

service MyService() {
	execution{ single }
	
	embed Runtime as Runtime
	embed Console as Console

	main {
		a = 10
		a.x = "hey"
		a.y = 20
		a.z = true

		b << a

		dumpState@Runtime()(s1)
		print@Console(s1)()
	}
}