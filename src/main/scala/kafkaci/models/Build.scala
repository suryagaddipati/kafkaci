package kafkaci.models

import kafkaci.models.github.GithubWebhook

case class Build(number: Long, webhook: GithubWebhook)
