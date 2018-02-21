package kafkaci.models

import kafkaci.models.github.GithubWebhook

case class Build(number: Long, webhook: GithubWebhook)
//this should be repo name
case class Job(name: String)
