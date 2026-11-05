/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.Vec3;

public class Path {
    private final List<Node> nodes;
    private Node[] openSet = new Node[0];
    private Node[] closedSet = new Node[0];
    @Nullable
    private Set<Target> targetNodes;
    private int nextNodeIndex;
    private final BlockPos target;
    private final float distToTarget;
    private final boolean reached;

    public Path(List<Node> list, BlockPos blockPos, boolean bl) {
        this.nodes = list;
        this.target = blockPos;
        this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
        this.reached = bl;
    }

    public void advance() {
        ++this.nextNodeIndex;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.size();
    }

    @Nullable
    public Node getEndNode() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size() - 1);
        }
        return null;
    }

    public Node getNode(int n) {
        return this.nodes.get(n);
    }

    public void truncateNodes(int n) {
        if (this.nodes.size() > n) {
            this.nodes.subList(n, this.nodes.size()).clear();
        }
    }

    public void replaceNode(int n, Node node) {
        this.nodes.set(n, node);
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getNextNodeIndex() {
        return this.nextNodeIndex;
    }

    public void setNextNodeIndex(int n) {
        this.nextNodeIndex = n;
    }

    public Vec3 getEntityPosAtNode(Entity entity, int n) {
        Node node = this.nodes.get(n);
        double d = (double)node.x + (double)((int)(entity.getBbWidth() + 1.0f)) * 0.5;
        double d2 = node.y;
        double d3 = (double)node.z + (double)((int)(entity.getBbWidth() + 1.0f)) * 0.5;
        return new Vec3(d, d2, d3);
    }

    public BlockPos getNodePos(int n) {
        return this.nodes.get(n).asBlockPos();
    }

    public Vec3 getNextEntityPos(Entity entity) {
        return this.getEntityPosAtNode(entity, this.nextNodeIndex);
    }

    public BlockPos getNextNodePos() {
        return this.nodes.get(this.nextNodeIndex).asBlockPos();
    }

    public Node getNextNode() {
        return this.nodes.get(this.nextNodeIndex);
    }

    @Nullable
    public Node getPreviousNode() {
        return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
    }

    public boolean sameAs(@Nullable Path path) {
        if (path == null) {
            return false;
        }
        if (path.nodes.size() != this.nodes.size()) {
            return false;
        }
        for (int i = 0; i < this.nodes.size(); ++i) {
            Node node = this.nodes.get(i);
            Node node2 = path.nodes.get(i);
            if (node.x == node2.x && node.y == node2.y && node.z == node2.z) continue;
            return false;
        }
        return true;
    }

    public boolean canReach() {
        return this.reached;
    }

    @VisibleForDebug
    void setDebug(Node[] nodeArray, Node[] nodeArray2, Set<Target> set) {
        this.openSet = nodeArray;
        this.closedSet = nodeArray2;
        this.targetNodes = set;
    }

    @VisibleForDebug
    public Node[] getOpenSet() {
        return this.openSet;
    }

    @VisibleForDebug
    public Node[] getClosedSet() {
        return this.closedSet;
    }

    public void writeToStream(FriendlyByteBuf friendlyByteBuf) {
        if (this.targetNodes == null || this.targetNodes.isEmpty()) {
            return;
        }
        friendlyByteBuf.writeBoolean(this.reached);
        friendlyByteBuf.writeInt(this.nextNodeIndex);
        friendlyByteBuf.writeInt(this.targetNodes.size());
        this.targetNodes.forEach(target -> target.writeToStream(friendlyByteBuf));
        friendlyByteBuf.writeInt(this.target.getX());
        friendlyByteBuf.writeInt(this.target.getY());
        friendlyByteBuf.writeInt(this.target.getZ());
        friendlyByteBuf.writeInt(this.nodes.size());
        for (Node node : this.nodes) {
            node.writeToStream(friendlyByteBuf);
        }
        friendlyByteBuf.writeInt(this.openSet.length);
        for (Node node : this.openSet) {
            node.writeToStream(friendlyByteBuf);
        }
        friendlyByteBuf.writeInt(this.closedSet.length);
        for (Node node : this.closedSet) {
            node.writeToStream(friendlyByteBuf);
        }
    }

    public static Path createFromStream(FriendlyByteBuf friendlyByteBuf) {
        boolean bl = friendlyByteBuf.readBoolean();
        int n = friendlyByteBuf.readInt();
        int n2 = friendlyByteBuf.readInt();
        HashSet hashSet = Sets.newHashSet();
        for (int i = 0; i < n2; ++i) {
            hashSet.add(Target.createFromStream(friendlyByteBuf));
        }
        BlockPos blockPos = new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
        ArrayList arrayList = Lists.newArrayList();
        int n3 = friendlyByteBuf.readInt();
        for (int i = 0; i < n3; ++i) {
            arrayList.add(Node.createFromStream(friendlyByteBuf));
        }
        Node[] nodeArray = new Node[friendlyByteBuf.readInt()];
        for (int i = 0; i < nodeArray.length; ++i) {
            nodeArray[i] = Node.createFromStream(friendlyByteBuf);
        }
        Node[] nodeArray2 = new Node[friendlyByteBuf.readInt()];
        for (int i = 0; i < nodeArray2.length; ++i) {
            nodeArray2[i] = Node.createFromStream(friendlyByteBuf);
        }
        Path path = new Path(arrayList, blockPos, bl);
        path.openSet = nodeArray;
        path.closedSet = nodeArray2;
        path.targetNodes = hashSet;
        path.nextNodeIndex = n;
        return path;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getDistToTarget() {
        return this.distToTarget;
    }
}

