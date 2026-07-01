import React, { useState, useEffect } from 'react';
import ItemService from '../services/itemService';
import { useNavigate } from 'react-router-dom';

const ItemList = () => {
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");

    useEffect(() => {
        fetchItems();
    }, []);

    const fetchItems = async () => {
        try {
            const res = await ItemService.getAllItems();
            setItems(res.data?.content || res.data || []);
        } catch (err) {
            console.error("Error fetching items", err);
        }
    };

    const filteredItems = items.filter(item => 
        item.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.location.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (item.reporterName && item.reporterName.toLowerCase().includes(searchTerm.toLowerCase()))
    );

    return (
        <div className="item-container">
            <div className="page-header">
                <h2>Reported Items</h2>
            </div>

            <input 
                type="text" 
                placeholder="Search items instantly..." 
                className="search-bar"
                onChange={(e) => setSearchTerm(e.target.value)} 
            />

            <div className="table-responsive">
                <table className="item-table">
                    <thead>
                        <tr>
                            <th>Title</th>
                            <th>Reported By</th>
                            <th>Location</th>
                            <th>Status</th>
                            <th className="center-text">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredItems.map(item => (
                            <tr 
                                key={item.id} 
                                onClick={() => navigate(`/item/${item.id}`)}
                                className="item-row-hover"
                            >
                                <td className="item-title-cell">{item.title}</td>
                                <td className="item-text-cell">{item.reporterName || 'Unknown'}</td>
                                <td className="item-text-cell muted">{item.location}</td>
                                <td>
                                    <span className={`status-badge ${item.found ? 'found' : 'lost'}`}>
                                        {item.found ? " Found" : " Lost"}
                                    </span>
                                </td>
                                <td className="center-text">
                                    <button className="action-btn link-text">
                                        View →
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            {filteredItems.length === 0 && (
                <div className="no-items-message">
                    No items found.
                </div>
            )}
        </div>
    );
};

export default ItemList;