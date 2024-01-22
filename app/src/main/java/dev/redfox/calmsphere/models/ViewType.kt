package dev.redfox.calmsphere.models

sealed class ViewType {
    class NormalView(val zenData: ZenDataModel) : ViewType()
    class EndView(val text: String): ViewType()
}