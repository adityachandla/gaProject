import matplotlib.pyplot as plt
import json

def read_input(file_name):
    with open(file_name, "r") as f:
        return json.loads(f.read())

def plot_points(pts, color):
    pts.append(pts[0])
    x = [p["x"] for p in pts]
    y = [p["y"] for p in pts]
    plt.plot(x, y, color=color)

def plot_instance(instance):
    plot_points(instance["outer_boundary"], "blue")
    for hole in instance["holes"]:
        plot_points(hole, "red")

def plot_lines(lines):
    for line in lines:
        x = (int(line["start"]["x"]), int(line["end"]["x"]))
        y = (int(line["start"]["y"]), int(line["end"]["y"]))
        plt.plot(x, y, color="green", marker="o")

def main():
    file_name = "srpg_octa_mc0000784.instance.json"
    instance = read_input(file_name)
    plot_instance(instance)
    line_name = "lines.json"
    lines = read_input(line_name)
    plot_lines(lines)
    plt.show()

if __name__ == "__main__":
    main()
