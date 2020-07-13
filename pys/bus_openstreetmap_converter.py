import numpy
from haversine import haversine, Unit
import sys
sys.setrecursionlimit(1000000)

# path of openstreetmap file
open_street_map_file = "map"
# path of out file
output_file = "HanoiCityRoad-connected.txt"
# parameter for using all connected components (1 using all components, 0 otherwise)
get_all_connected_components = 0
# boundary of region
lat_lower = 20.9950
lat_upper = 21.0471
lng_lower = 105.7685
lng_upper = 105.8703

id = 0
vertex_to_info = dict()
vertex_to_id = dict()
edges = dict()
node_to_node_lst = dict()
m_lat_lng_2_id = {}

num = 0
low = [0] * 1000000
cnt = [0] * 1000000
stack = []
group = [0] * 1000000
group_size = [0] * 1000000
nb_group = 0

def dfs(u):
    global num
    global nb_group
    num += 1
    cnt[u] = low[u] = num
    stack.append(u)
    if u in node_to_node_lst.keys():
        for v in node_to_node_lst[u]:
            if group[v] == 0:
                if cnt[v] == 0:
                    dfs(v)
                    low[u] = min(low[u], low[v])
                else:
                    low[u] = min(low[u], cnt[v])
    
    if low[u] == cnt[u]:
        nb_group += 1
        while stack[-1] != u:
            group[stack[-1]] = nb_group
            group_size[nb_group] += 1
            stack.pop(-1)
        group_size[nb_group] += 1
        group[u] = nb_group
        stack.pop(-1)

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
    # add_node(u)
    # add_node(v)
    node_to_node_lst[u].append(v)
    # node_to_node_lst[v].append(u)


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
                if (lat, lng) not in m_lat_lng_2_id.keys():
                    id += 1
                    vertex_to_info[id] = (lat, lng)
                    m_lat_lng_2_id[(lat, lng)] = id
                    vertex_to_id[numpy.int64(node_id)] = id
                    print("node", id, node_id, lat_st, lon_st)
                    node_to_node_lst[id] = list()
                else:
                    vertex_to_id[numpy.int64(node_id)] = m_lat_lng_2_id[(lat, lng)]

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
                        add_edge(v, u)
                        edges[(v, u)] = dis
                    print(vertex_to_info[u], vertex_to_info[v], dis)
        line = f.readline()

dfs_stack = []
v_stack = []
for _u in range(1, id + 1):
    print("Tarjan", _u)
    if cnt[_u] > 0:
        continue
    dfs_stack.append(_u)
    v_stack.append(0)
    while True:
        u = dfs_stack[-1]
        if v_stack[-1] == 0:
            num += 1
            cnt[u] = low[u] = num
            stack.append(u)
        v_lst = node_to_node_lst[u]
        if v_stack[-1] < len(v_lst):
            v = v_lst[v_stack[-1]]
            v_stack[-1] += 1
            if group[v] == 0:
                if cnt[v] == 0:
                    dfs_stack.append(v)
                    v_stack.append(0)
                else:
                    low[u] = min(low[u], cnt[v])
        else:
            if low[u] == cnt[u]:
                nb_group += 1
                while stack[-1] != u:
                    group[stack[-1]] = nb_group
                    group_size[nb_group] += 1
                    stack.pop(-1)
                group_size[nb_group] += 1
                group[u] = nb_group
                stack.pop(-1)
            dfs_stack.pop(-1)
            v_stack.pop(-1)
            if len(dfs_stack) > 0:
                low[dfs_stack[-1]] = min(low[dfs_stack[-1]], low[u])
            else:
                break
    # dfs(u)

max_group = 1
for u in range(2, nb_group + 1):
    if group_size[u] > group_size[max_group]:
        max_group = u

print("write_file")
with open(output_file, "w") as f:
    for id, (lat, lng) in vertex_to_info.items():
        if group[id] == max_group:
            f.write(str(id) + " " + str(lat) + " " + str(lng) + "\n")
    f.write("-1\n")
    for (u, v), dis in edges.items():
        if group[u] == group[v] == max_group:
            f.write(str(u) + " " + str(v) + " " + str(dis) + "\n")
    f.write("-1\n")

