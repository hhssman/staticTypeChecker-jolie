from .types import C, ImportedInterface
from .otherservice import EmbedMe, EmbedMeInterface1, EmbedMeInterface2

type A: void {
	tmp: C
}

type B: void {
	name: string
}

// type aliasA: A

// type RecursiveType: int {
// 	SubRec: RecursiveType
// }

// type ChoiceType: int | A | string

interface MyInterface {
	RequestResponse:
		helloReqRes( B )( string )
	OneWay:
		helloOneway( A )
}

service MyService(param: any) {
	execution{ concurrent }

	// outputPort EmbedMePort {
	// 	Interfaces: EmbedMeInterface
	// }

	embed EmbedMe(10) as EmbedMePort

	inputPort MyInputPort {
		Location: "socket://localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	outputPort MyOutputPort {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: ImportedInterface
	}

	main {
		helloReqRes( input )( output ){
			output = "Hello from test.ol" + input.name
			embedHelloReqRes@EmbedMePort(output)(output)
		}

		helloOneway( input )
	}
}