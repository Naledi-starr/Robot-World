import sys

def bump(version, kind):
    parts = version.strip().split("-")[0].split(".")
    parts = [int(p) for p in parts]
    while len(parts) < 3:
        parts.append(0)

    if kind == "major":
        parts[0] += 1
    elif kind == "minor":
        parts[1] += 1
    elif kind == "patch":
        parts[2] += 1
    else:
        raise ValueError("Unknown version bump kind: " + kind)

    print("{}.{}.{}".format(*parts))

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: bump_version.py <version> <patch|minor|major>")
        sys.exit(1)
    bump(sys.argv[1], sys.argv[2])
