plugins {
    id("myproject.jacoco-aggregation")
}

dependencies {
    jacocoAggregation(project(":application"))
}
