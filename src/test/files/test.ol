from .types import C, ImportedInterface
from .otherservice import EmbedMe

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

service MyService(param: any) {
	execution{ concurrent }

	embed EmbedMe as EmbedMeInterface

	inputPort MyInputPort {
		Location: "localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	outputPort MyOutputPort {
		Location: "localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: ImportedInterface
	}

	main {
		helloReqRes( input )( output ){
			output = "Hello!" + input.name
		}

		helloOneway( input )
	}
}