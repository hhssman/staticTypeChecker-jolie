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

type A: any {
	choice: B
}

type B: int { intSub: int } | string { stringSub: string }

service MyService(param: A) {
	main {
		// a = 10
		// a = "10"
		// c = 10 + 1
		// d = 10.1
		// e = a.f.e

		param.choice.intSub = 10
		param.choice.stringSub = "10"

		// a.b.c = 10
		// nullProcess
		// undef(a.b.c)
		// undef(a.b)
		// undef(a)
		// undef(param)
	}
}