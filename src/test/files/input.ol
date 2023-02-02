// from .types import C, ImportedInterface
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

// type A: void {
// 	tmp: C
// }

// type B: void {
// 	name: string
// }

// // type aliasA: A

// // type RecursiveType: int {
// // 	SubRec: RecursiveType
// // }

// // type ChoiceType: int | A | string

// interface MyInterface {
// 	RequestResponse:
// 		helloReqRes( B )( string )
// 	OneWay:
// 		helloOneway( A )
// }

// service MyService(param: any) {
// 	execution{ concurrent }

// 	outputPort EmbedInPort {
// 		Interfaces: EmbedMeInterface1
// 	}

// 	embed EmbedInService(10) in EmbedInPort
// 	embed EmbedAsService(10) as EmbedAsPort

// 	inputPort MyInputPort {
// 		Location: "socket://localhost:8080"
// 		Protocol: http { format = "json" }
// 		Interfaces: MyInterface
// 	}

// 	outputPort MyOutputPort {
// 		Location: "socket://localhost:8081"
// 		Protocol: http { format = "json" }
// 		Interfaces: ImportedInterface
// 	}

// 	main {
// 		helloReqRes( input )( output ){
// 			output = "Hello from test.ol" + input.name
// 			embedHelloReqRes@EmbedAsPort(output)(output)
// 		}

// 		helloOneway( input )
// 	}
// }

type A: int {
	choice: int | string
}

type B: string {
	s: string
}

type C: int {
	i: int
}

service MyService(param: A) {
	main {
		param.choice.k = 10
	}
}