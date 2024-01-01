[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

### Panorama Discord Broadcaster

Listens for new posts at https://panorama.pub (Russian) and broadcasts them into a Discord channel via configured
webhook.

### Table of Contents

- [Configuration](#Configuration)
- [Running](#Running)
- [License](#License)

### Configuration

The application is being configured through the `.env` file, which is
being loaded as environment variables.

Open the `.env.tmp` file and start to edit the configuration.

The following environment variables are available:

| Environment variable                                              | Description                                          |
|-------------------------------------------------------------------|------------------------------------------------------|
| `PANORAMA_DISCORD_BROADCASTER__REDIS__ENDPOINT`                   | Redis endpoint to connect to                         |
| `PANORAMA_DISCORD_BROADCASTER__WEBHOOK__URL`                      | Webhook URL to use                                   |
| `PANORAMA_DISCORD_BROADCASTER__FETCHER__DELAY`                    | Fetching delay                                       |
| `PANORAMA_DISCORD_BROADCASTER__YANDEX_CLOUD__API_KEY`             | Optional. Yandex Cloud API key for a service account |
| `PANORAMA_DISCORD_BROADCASTER__YANDEX_CLOUD__FOLDER_ID`           | Optional. Yandex Cloud folder ID                     |
| `PANORAMA_DISCORD_BROADCASTER__TRANSLATION__TARGET_LANGUAGE_CODE` | Optional. Target language to translate to            |

Save the file as `.env` stripping the `.tmp` suffix.

### Running

The preferred way to run the application is using Docker Compose:

```shell
./gradlew installDist
docker-compose -f ./docker/docker-compose.yml up --build
```

Docker configuration is located at `./docker`

### Code of Conduct

See [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md).

### License

```
Copyright 2023-2024 Mikhail Titov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
