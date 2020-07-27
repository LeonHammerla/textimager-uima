# abort at error
set -e

# params from java
CONDA_INSTALL_DIR="$1"
ENV_NAME="$2"

# activate "base" conda
echo "activating conda installation in $CONDA_INSTALL_DIR"
source "$CONDA_INSTALL_DIR/etc/profile.d/conda.sh"
conda activate

# activate env
conda activate "$ENV_NAME"

# install models
echo "installing spacy models..."
python3 -m spacy download de_core_news_sm
python3 -m spacy download en_core_news_sm
