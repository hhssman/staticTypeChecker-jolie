from .types import B
// type TypeAlias: InputType

type A: void {
	tmp: B
}


// type SubType: int {
// 	subtypeName: string
// }

// type RecursiveType: int {
// 	SubRec: RecursiveType
// }

// type ChoiceType: int | InputType | string

interface MyInterface {
	RequestResponse:
		// hello( InputType )( string )
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