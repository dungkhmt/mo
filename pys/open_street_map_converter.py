import numpy
from haversine import haversine, Unit

# path of openstreetmap file
open_street_map_file = "malaysia-singapore-brunei-latest.osm"
# path of out file
output_file = "MalaysiaRoad-connected.txt"
# parameter for using all connected components (1 using all components, 0 otherwise)
get_all_connected_components = 0
# boundary of region
lat_lower = 0
lat_upper = 1e9
lng_lower = 0
lng_upper = 1e9

id = 0
vertex_to_info = dict()
vertex_to_id = dict()
edges = dict()
node_to_node_lst = dict()

def get_val_st(st, key, start_pos=0):
    pos = st.find(key, start_pos, len(st))
    if pos < 0:
        return ""
    pos += len(key)
    while st[pos] != "\"":
        pos += 1
    pos += 1
    num_st = ""
    while st[pos] != "\"":
        num_st += st[pos]
        pos += 1
    return num_st

def add_node(u):
    if u not in node_to_node_lst.keys():
        node_to_node_lst[u] = list()

def add_edge(u, v):
    add_node(u)
    add_node(v)
    node_to_node_lst[u].append(v)
    node_to_node_lst[v].append(u)


with open(open_street_map_file, encoding="utf8") as f:
    line = f.readline()
    while line:
        # print(line)
        if "<node id=" in line:
            node_st = "" + line
            while "</node>" not in line and "/>" not in line:
                line = f.readline()
                node_st += line
                if not line:
                    break

            node_id = get_val_st(node_st, "<node id=")
            lat_st = get_val_st(node_st, "lat=")
            lon_st = get_val_st(node_st, "lon=")
            lat = numpy.float(lat_st)
            lng = numpy.float(lon_st)
            if lat_lower <= lat <= lat_upper and lng_lower <= lng <= lng_upper:
                id += 1
                vertex_to_info[id] = (lat, lng)
                vertex_to_id[numpy.int64(node_id)] = id
                print("node", id, node_id, lat_st, lon_st)
            if not line:
                break
        if "<way id=" in line:
            way_st = "" + line
            while "</way>" not in line:
                line = f.readline()
                if "<way id=" in line:
                    way_st = ""
                way_st += line
                if not line:
                    break
            start_pos = 0
            vertex_lst = list()
            while way_st.find("<nd ref=", start_pos, len(way_st)) >= start_pos:
                node_id = get_val_st(way_st, "<nd ref=", start_pos)
                vertex_lst.append(numpy.int64(node_id))
                start_pos = way_st.find("<nd ref=", start_pos, len(way_st)) + len("<nd ref=")
            # print(vertex_lst)
            oneway_st = get_val_st(way_st, "<tag k=\"oneway\" v=")
            oneway = False
            if oneway_st == "yes":
                print("oneway")
                oneway = True

            check = True
            for u in vertex_lst:
                if u not in vertex_to_id:
                    check = False
                    break
            if check:
                for i in range(len(vertex_lst) - 1):
                    u = vertex_to_id[vertex_lst[i]]
                    v = vertex_to_id[vertex_lst[i + 1]]
                    print(u, v)
                    add_edge(u, v)
                    dis = haversine(vertex_to_info[u], vertex_to_info[v]) * 1000
                    edges[(u, v)] = dis
                    if not oneway:
                        edges[(v, u)] = dis
                    print(vertex_to_info[u], vertex_to_info[v], dis)
        line = f.readline()

if get_all_connected_components:
    with open(output_file, "w") as f:
        for id, (lat, lng) in vertex_to_info.items():
            f.write(str(id) + " " + str(lat) + " " + str(lng) + "\n")
        f.write("-1\n")
        for (u, v), dis in edges.items():
            f.write(str(u) + " " + str(v) + " " + str(dis) + "\n")
        f.write("-1\n")

    exit(0)

print(len(edges))
mark = set()
q = [None] * len(vertex_to_info)
best_vertex_set = set()
for u in node_to_node_lst.keys():
    if u in mark:
        continue

    l = r = 0
    q[r] = u
    r += 1
    mark.add(u)
    while l < r:
        u = q[l]
        l += 1
        for v in node_to_node_lst[u]:
            if v not in mark:
                mark.add(v)
                q[r] = v
                r += 1

    if len(best_vertex_set) < r:
        best_vertex_set = set()
        for i in range(r):
            best_vertex_set.add(q[i])

print(len(best_vertex_set), best_vertex_set)
id = 0
id_dict = dict()
for u in best_vertex_set:
    id += 1
    id_dict[u] = id
with open(output_file, "w") as f:
    for id, (lat, lng) in vertex_to_info.items():
        if id in best_vertex_set:
            f.write(str(id_dict[id]) + " " + str(lat) + " " + str(lng) + "\n")
    f.write("-1\n")
    for (u, v), dis in edges.items():
        if u in best_vertex_set and v in best_vertex_set:
            f.write(str(id_dict[u]) + " " + str(id_dict[v]) + " " + str(dis) + "\n")
    f.write("-1\n")
