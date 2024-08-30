#!/bin/bash

JUPYTERLAB_PASSWORD=

# Generate the hashed password
HASHED_PASSWORD=$(python3 -c "from jupyter_server.auth import passwd; print(passwd(passphrase='$JUPYTERLAB_PASSWORD', algorithm='sha1'))")

# Execute JupyterLab with additional options
# --ServerApp.root_dir=/ --ServerApp.preferred_dir=/opt/notebooks
# --ServerApp.custom_display_url='http://localhost:8081/'
# --ServerApp.websocket_url='ws://localhost:8081/'
ADDITIONAL_OPTIONS="--ServerApp.disable_check_xsrf=True --ServerApp.allow_origin='*' --ServerApp.trust_xheaders=True --ServerApp.allow_remote_access=True"
jupyter-lab --port=9999 --ip=0.0.0.0 --no-browser --allow-root --ServerApp.password="$HASHED_PASSWORD" $ADDITIONAL_OPTIONS
