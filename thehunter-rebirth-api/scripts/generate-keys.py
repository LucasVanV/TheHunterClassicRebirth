from pathlib import Path
import subprocess
import sys


ROOT = Path(__file__).resolve().parent.parent

SECRETS_DIR = ROOT / "secrets"
TEST_RESOURCES_DIR = ROOT / "src" / "test" / "resources"


def generate_keys(directory: Path):
    private_key = directory / "privateKey.pem"
    public_key = directory / "publicKey.pem"

    directory.mkdir(parents=True, exist_ok=True)

    if not private_key.exists():
        subprocess.run(
            [
                "openssl",
                "genpkey",
                "-algorithm",
                "RSA",
                "-out",
                str(private_key),
                "-pkeyopt",
                "rsa_keygen_bits:2048",
            ],
            check=True,
        )

        print(f"Private key generated: {private_key}")
    else:
        print(f"Private key already exists: {private_key}")

    if not public_key.exists():
        subprocess.run(
            [
                "openssl",
                "pkey",
                "-in",
                str(private_key),
                "-pubout",
                "-out",
                str(public_key),
            ],
            check=True,
        )

        print(f"Public key generated: {public_key}")
    else:
        print(f"Public key already exists: {public_key}")


def main():
    if len(sys.argv) > 1 and sys.argv[1] == "test":
        generate_keys(TEST_RESOURCES_DIR)
        print("JWT test keys are ready.")
        return

    generate_keys(SECRETS_DIR)
    print("JWT application keys are ready.")


if __name__ == "__main__":
    main()