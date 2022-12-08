type InputType: void {
	name: string
}

interface MyInterface {
	RequestResponse:
		hello( InputType )( string )
}

service MyService {
	execution{ concurrent }

	inputPort PortName {
		Location: "localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		hello( input )( output ){
			output = "Hello!" + input.name
		}
	}
}