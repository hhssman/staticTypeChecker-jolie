service Main {
    main {
        a = 10
		for(i = 0, i < 3, i++){
			a = 10
			b = "hey"

			if(i == 2){
				c = true
				a = false
			}
		}
    }
}