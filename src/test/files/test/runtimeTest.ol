from runtime import Runtime
from console import Console

interface MyInterface{
	OneWay:
		submit(string),
		submit2(string)
}

service MyService() {
	execution{ sequential }

	embed Runtime as Runtime
	embed Console as Console

	inputPort port {
		location: "socket://localhost:8080"
		protocol: http { format = "json" }
		interfaces: MyInterface
	}

	main {
		[submit(m)]{m = "lol"}
		[submit2(m)]{m = "lol2"}
		// println@Console("hello from before the choice")()


		// println@Console("hello from after the choice")()

		// dumpState@Runtime()(s1)
		// print@Console(s1)()
	}
}