from .types import C

type A: void {
	tmp: C
}

// type aliasA: A

// type RecursiveType: int {
// 	SubRec: RecursiveType
// }

// type ChoiceType: int | A | string

interface MyInterface {
	RequestResponse:
		helloReqRes( A )( string )
	OneWay:
		helloOneway( A )
}

service MyService {
	execution{ concurrent }

	inputPort MyInputPort {
		Location: "localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		helloReqRes( input )( output ){
			output = "Hello!" + input.name
		}

		helloOneway( input )
	}
}