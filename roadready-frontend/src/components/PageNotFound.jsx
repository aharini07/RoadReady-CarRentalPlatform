import { useEffect } from "react"

function PageNotFound(){
    useEffect(()=>{
        localStorage.clear()
    },[])
    return(
        <div>
            <h1>
                Page Not Found
            </h1>
        </div>
    )
}

export default PageNotFound