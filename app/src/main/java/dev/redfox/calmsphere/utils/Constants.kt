package dev.redfox.calmsphere.utils

class Constants {
    companion object {
        const val BASE_URL = "https://m67m0xe4oj.execute-api.us-east-1.amazonaws.com/"

        //Exception Messages
        const val NO_NETWORK_EXCEPTION = "No Internet Connection!"
        const val HTTP_UNAUTHORIZED_EXCEPTION = "Unauthorized: User authentication failed."
        const val HTTP_NOT_FOUND_EXCEPTION = "Not Found: The requested resource could not be found."
    }
}